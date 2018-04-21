/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.query;

import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.spring.data.gremlin.common.GremlinPropertiesConfiguration;
import com.microsoft.spring.data.gremlin.common.TestConstants;
import com.microsoft.spring.data.gremlin.common.domain.Network;
import com.microsoft.spring.data.gremlin.common.domain.Person;
import com.microsoft.spring.data.gremlin.common.domain.Project;
import com.microsoft.spring.data.gremlin.common.domain.Relationship;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.exception.GremlinQueryException;
import com.microsoft.spring.data.gremlin.mapping.GremlinMappingContext;
import lombok.SneakyThrows;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource(value = {"classpath:application.properties"})
@EnableConfigurationProperties(GremlinPropertiesConfiguration.class)
public class GremlinTemplateIT {

    private final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
    private final Person person0 = new Person(TestConstants.VERTEX_PERSON_0_ID, TestConstants.VERTEX_PERSON_0_NAME);
    private final Person person1 = new Person(TestConstants.VERTEX_PERSON_1_ID, TestConstants.VERTEX_PERSON_1_NAME);
    private final Project project = new Project(TestConstants.VERTEX_PROJECT_ID, TestConstants.VERTEX_PROJECT_NAME,
            TestConstants.VERTEX_PROJECT_URI);
    private final Project project0 = new Project(TestConstants.VERTEX_PROJECT_0_ID, TestConstants.VERTEX_PROJECT_0_NAME,
            TestConstants.VERTEX_PROJECT_0_URI);
    private final Relationship relationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_ID,
            TestConstants.EDGE_RELATIONSHIP_NAME, TestConstants.EDGE_RELATIONSHIP_LOCATION,
            this.person, this.project);
    private final Relationship relationship0 = new Relationship(TestConstants.EDGE_RELATIONSHIP_0_ID,
            TestConstants.EDGE_RELATIONSHIP_0_NAME, TestConstants.EDGE_RELATIONSHIP_0_LOCATION,
            this.person0, this.project);
    private final Relationship relationship1 = new Relationship(TestConstants.EDGE_RELATIONSHIP_1_ID,
            TestConstants.EDGE_RELATIONSHIP_1_NAME, TestConstants.EDGE_RELATIONSHIP_1_LOCATION,
            this.person1, this.project);
    private final Relationship relationship2 = new Relationship(TestConstants.EDGE_RELATIONSHIP_2_ID,
            TestConstants.EDGE_RELATIONSHIP_2_NAME, TestConstants.EDGE_RELATIONSHIP_2_LOCATION,
            this.person, this.project0);

    private final Network network = new Network();

    @Autowired
    private GremlinPropertiesConfiguration config;

    @Autowired
    private ApplicationContext context;

    private static GremlinFactory factory;
    private GremlinTemplate template;

    @AfterClass
    public static void closeResource() {
        factory.getGremlinCluster().close();
    }

    @Before
    @SneakyThrows
    public void setup() {
        final GremlinMappingContext mappingContext = new GremlinMappingContext();
        factory = new GremlinFactory(this.config.getEndpoint(), this.config.getPort(),
                this.config.getUsername(), this.config.getPassword());

        mappingContext.setInitialEntitySet(new EntityScanner(this.context).scan(Persistent.class));

        final MappingGremlinConverter converter = new MappingGremlinConverter(mappingContext);

        this.template = new GremlinTemplate(factory, converter);
        this.template.deleteAll();
    }

    private void buildTestGraph() {
        this.network.vertexAdd(this.person);
        this.network.vertexAdd(this.person0);
        this.network.vertexAdd(this.person1);
        this.network.vertexAdd(this.project);
        this.network.vertexAdd(this.project0);

        this.network.edgeAdd(this.relationship);
        this.network.edgeAdd(this.relationship0);
        this.network.edgeAdd(this.relationship1);
        this.network.edgeAdd(this.relationship2);

        this.template.insert(this.network);
    }

    @After
    public void cleanup() {
        this.template.deleteAll();
    }

    @Test
    @SneakyThrows
    public void testVertexDeleteAll() {
        this.buildTestGraph();

        Person personVertex = this.template.findVertexById(this.person.getId(), Person.class);
        Project projectVertex = this.template.findVertexById(this.project.getId(), Project.class);
        Relationship relationshipEdge = this.template.findEdgeById(this.relationship.getId(), Relationship.class);

        Assert.assertNotNull(personVertex);
        Assert.assertNotNull(projectVertex);
        Assert.assertNotNull(relationshipEdge);

        this.template.deleteAll();

        personVertex = this.template.findVertexById(this.person.getId(), Person.class);
        projectVertex = this.template.findVertexById(this.project.getId(), Project.class);
        relationshipEdge = this.template.findEdgeById(this.relationship.getId(), Relationship.class);

        Assert.assertNull(personVertex);
        Assert.assertNull(projectVertex);
        Assert.assertNull(relationshipEdge);

        // Todo(pan): should add findVertexAll here.
    }

    @Test
    public void testVertexInsertNormal() {
        this.template.insert(this.person0);

        final Person foundPerson = this.template.findVertexById(this.person0.getId(), Person.class);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person0.getId());
        Assert.assertEquals(foundPerson.getName(), this.person0.getName());
    }

    @Test(expected = GremlinQueryException.class)
    public void testVertexInsertException() {
        this.template.insert(this.person);

        final Person repeated = new Person(this.person.getId(), this.person.getName());

        this.template.insert(repeated);
    }

    @Test
    public void testEdgeInsertNormal() {
        this.template.insert(this.person);
        this.template.insert(this.project);
        this.template.insert(this.relationship);

        final Relationship foundRelationship = this.template.findById(this.relationship.getId(), Relationship.class);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship.getLocation());
    }

    @Test(expected = GremlinQueryException.class)
    public void testEdgeInsertException() {
        this.template.insert(this.person);
        this.template.insert(this.project);
        this.template.insert(this.relationship);

        final Relationship repeated = new Relationship(this.relationship.getId(), this.relationship.getName(),
                this.relationship.getLocation(), this.person, this.project);

        this.template.insert(repeated);
    }

    @Test
    public void testFindVertexById() {
        Person foundPerson = this.template.findVertexById(this.person1.getId(), Person.class);
        Assert.assertNull(foundPerson);

        this.template.insert(this.person1);

        foundPerson = this.template.findVertexById(this.person1.getId(), Person.class);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person1.getId());
        Assert.assertEquals(foundPerson.getName(), this.person1.getName());
    }

    @Test
    public void testFindEdgeById() {
        Relationship foundRelationship = this.template.findEdgeById(this.relationship2.getId(), Relationship.class);
        Assert.assertNull(foundRelationship);

        this.template.insert(this.person);
        this.template.insert(this.project0);
        this.template.insert(this.relationship2);

        foundRelationship = this.template.findEdgeById(this.relationship2.getId(), Relationship.class);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship2.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship2.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship2.getLocation());
    }

    @Test
    public void testFindById() {
        this.buildTestGraph();
        final Person foundPerson = this.template.findById(this.person1.getId(), Person.class);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person1.getId());
        Assert.assertEquals(foundPerson.getName(), this.person1.getName());

        final Relationship foundRelationship = this.template.findById(this.relationship.getId(), Relationship.class);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship.getLocation());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindByIdException() {
        this.template.findById(this.network.getId(), Network.class);
    }

    @Test(expected = GremlinQueryException.class)
    public void testUpdateException() {
        this.template.update(this.person);
    }

    @Test
    public void testUpdateVertex() {
        this.template.insert(this.person);

        final String updatedName = "updated-person-name";
        final Person updatedPerson = new Person(this.person.getId(), updatedName);

        this.template.update(updatedPerson);

        final Person foundPerson = this.template.findById(updatedPerson.getId(), Person.class);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(this.person.getId(), foundPerson.getId());
        Assert.assertEquals(updatedPerson.getId(), foundPerson.getId());
        Assert.assertEquals(updatedPerson.getName(), foundPerson.getName());
    }

    @Test
    public void testUpdateEdge() {
        this.template.insert(this.person);
        this.template.insert(this.project0);
        this.template.insert(this.relationship2);

        final String updatedName = "updated-relation-name";
        final String updatedLocation = "updated-location";
        final Relationship updatedRelationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_2_ID,
                updatedName, updatedLocation, this.person, this.project0);

        this.template.update(updatedRelationship);

        final Relationship foundRelationship = this.template.findById(updatedRelationship.getId(), Relationship.class);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(this.relationship2.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getName(), foundRelationship.getName());
        Assert.assertEquals(updatedRelationship.getLocation(), foundRelationship.getLocation());
    }

    @Test
    public void testUpdateGraph() {
        this.buildTestGraph();

        final String updatedName = "update-person-name";
        final String updatedLocation = "update-location";
        final String updatedUri = "http://localhost:2222";

        final Person person = (Person) this.network.getVertexList().get(0);
        final Project project = (Project) this.network.getVertexList().get(3);
        final Relationship relationship = (Relationship) this.network.getEdgeList().get(0);

        person.setName(updatedName);
        project.setUri(updatedUri);
        relationship.setLocation(updatedLocation);

        this.template.update(network);

        final Person foundPerson = this.template.findById(person.getId(), Person.class);
        final Project foundProject = this.template.findById(project.getId(), Project.class);
        final Relationship foundRelationship = this.template.findById(relationship.getId(), Relationship.class);

        Assert.assertNotNull(foundPerson);
        Assert.assertNotNull(foundProject);
        Assert.assertNotNull(foundRelationship);

        Assert.assertEquals(foundPerson.getId(), person.getId());
        Assert.assertEquals(foundPerson.getName(), person.getName());

        Assert.assertEquals(foundProject.getId(), project.getId());
        Assert.assertEquals(foundProject.getUri(), project.getUri());

        Assert.assertEquals(foundRelationship.getId(), relationship.getId());
        Assert.assertEquals(foundRelationship.getLocation(), relationship.getLocation());
    }

    @Test
    public void testSaveVertex() {
        this.template.save(this.person);

        Person foundPerson = this.template.findById(this.person.getId(), Person.class);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person.getId());
        Assert.assertEquals(foundPerson.getName(), this.person.getName());

        final String updatedName = "update-person-name";
        final Person updatedPerson = new Person(this.person.getId(), updatedName);

        this.template.save(updatedPerson);

        foundPerson = this.template.findById(updatedPerson.getId(), Person.class);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), updatedPerson.getId());
        Assert.assertEquals(foundPerson.getName(), updatedPerson.getName());
    }

    @Test
    public void testSaveEdge() {
        this.template.insert(this.person);
        this.template.insert(this.project);
        this.template.save(this.relationship);

        Relationship foundRelationship = this.template.findById(this.relationship.getId(), Relationship.class);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship.getLocation());

        final String updatedName = "updated-relation-name";
        final String updatedLocation = "updated-location";
        final Relationship updatedRelationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_2_ID,
                updatedName, updatedLocation, this.person, this.project);

        this.template.save(updatedRelationship);

        foundRelationship = this.template.findById(updatedRelationship.getId(), Relationship.class);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(this.relationship2.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getName(), foundRelationship.getName());
        Assert.assertEquals(updatedRelationship.getLocation(), foundRelationship.getLocation());
    }

    @Test
    public void testVertexDeleteById() {
        this.template.deleteById(this.person.getId(), Person.class);
        this.template.insert(this.person);
        this.template.deleteById(this.person0.getId(), Person.class);

        Person foundPerson = this.template.findById(this.person.getId(), Person.class);
        Assert.assertNotNull(foundPerson);

        this.template.deleteById(this.person.getId(), Person.class);

        foundPerson = this.template.findById(this.person.getId(), Person.class);
        Assert.assertNull(foundPerson);
    }

    @Test
    public void testEdgeDeleteById() {
        this.template.deleteById(this.relationship.getId(), Relationship.class);

        this.template.insert(this.person);
        this.template.insert(this.project);
        this.template.insert(this.relationship);

        this.template.deleteById(this.relationship0.getId(), Relationship.class);

        Relationship foundRelationship = this.template.findById(this.relationship.getId(), Relationship.class);
        Assert.assertNotNull(foundRelationship);

        this.template.deleteById(this.relationship.getId(), Relationship.class);

        foundRelationship = this.template.findById(this.relationship.getId(), Relationship.class);
        Assert.assertNull(foundRelationship);
    }

    @Test
    public void testGraphDeleteById() {
        this.network.setId(this.config.getUsername());
        this.template.deleteById(this.network.getId(), Relationship.class);

        final Relationship foundRelationship = this.template.findById(this.relationship, Relationship.class);
        Assert.assertNull(foundRelationship);

        final Person foundPerson = this.template.findById(this.person.getId(), Person.class);
        Assert.assertNull(foundPerson);
    }
}


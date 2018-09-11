package com.structurizr.api;

import com.structurizr.Workspace;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.encryption.EncryptedWorkspace;
import com.structurizr.io.json.EncryptedJsonReader;
import com.structurizr.io.json.JsonReader;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StructurizrClientIntegrationTests {

    private StructurizrClient structurizrClient;
    private File workspaceArchiveLocation = new File(System.getProperty("java.io.tmpdir"), "structurizr");

    @Before
    public void setUp() {
        structurizrClient = new StructurizrClient("81ace434-94a1-486f-a786-37bbeaa44e08", "a8673e21-7b6f-4f52-be65-adb7248be86b");
        structurizrClient.setWorkspaceArchiveLocation(workspaceArchiveLocation);
        workspaceArchiveLocation.mkdirs();
        clearWorkspaceArchive();
        assertEquals(0, workspaceArchiveLocation.listFiles().length);
        structurizrClient.setMergeFromRemote(false);
    }

    @After
    public void tearDown() {
        assertEquals(1, workspaceArchiveLocation.listFiles().length);

        clearWorkspaceArchive();
        workspaceArchiveLocation.delete();
    }

    private void clearWorkspaceArchive() {
        if (workspaceArchiveLocation.listFiles() != null) {
            for (File file : workspaceArchiveLocation.listFiles()) {
                file.delete();
            }
        }
    }

    private File getArchivedWorkspace() {
        return workspaceArchiveLocation.listFiles()[0];
    }

    @Test
    public void test_putAndGetWorkspace_WithoutEncryption() throws Exception {
        Workspace workspace = new Workspace("Structurizr client library tests - without encryption", "A test workspace for the Structurizr client library");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System", "Description");
        Person person = workspace.getModel().addPerson("Person", "Description");
        person.uses(softwareSystem, "Uses");
        SystemContextView systemContextView = workspace.getViews().createSystemContextView(softwareSystem, "SystemContext", "Description");
        systemContextView.addAllElements();

        structurizrClient.putWorkspace(20081, workspace);

        workspace = structurizrClient.getWorkspace(20081);
        assertTrue(workspace.getModel().contains(softwareSystem));
        assertTrue(workspace.getModel().contains(person));
        assertEquals(1, workspace.getModel().getRelationships().size());
        assertEquals(1, workspace.getViews().getSystemContextViews().size());

        // and check the archive version is readable
        Workspace archivedWorkspace = new JsonReader().read(new FileReader(getArchivedWorkspace()));
        assertEquals(20081, archivedWorkspace.getId());
        assertEquals("Structurizr client library tests - without encryption", archivedWorkspace.getName());
        assertEquals(1, archivedWorkspace.getModel().getSoftwareSystems().size());
    }

    @Test
    public void test_putAndGetWorkspace_WithEncryption() throws Exception {
        structurizrClient.setEncryptionStrategy(new AesEncryptionStrategy("password"));
        Workspace workspace = new Workspace("Structurizr client library tests - with encryption", "A test workspace for the Structurizr client library");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Software System", "Description");
        Person person = workspace.getModel().addPerson("Person", "Description");
        person.uses(softwareSystem, "Uses");
        SystemContextView systemContextView = workspace.getViews().createSystemContextView(softwareSystem, "SystemContext", "Description");
        systemContextView.addAllElements();

        structurizrClient.putWorkspace(20081, workspace);

        workspace = structurizrClient.getWorkspace(20081);
        assertTrue(workspace.getModel().contains(softwareSystem));
        assertTrue(workspace.getModel().contains(person));
        assertEquals(1, workspace.getModel().getRelationships().size());
        assertEquals(1, workspace.getViews().getSystemContextViews().size());

        // and check the archive version is readable
        EncryptedWorkspace archivedWorkspace = new EncryptedJsonReader().read(new FileReader(getArchivedWorkspace()));
        assertEquals(20081, archivedWorkspace.getId());
        assertEquals("Structurizr client library tests - with encryption", archivedWorkspace.getName());
        assertTrue(archivedWorkspace.getEncryptionStrategy() instanceof AesEncryptionStrategy);
    }

}
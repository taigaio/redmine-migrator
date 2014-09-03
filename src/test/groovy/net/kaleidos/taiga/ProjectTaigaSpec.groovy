package net.kaleidos.taiga

class ProjectTaigaSpec extends TaigaSpecBase {

    void 'get a project by id'() {
        given: 'an existing project'
            def project = taigaClient.createProject("name ${new Date().time}", "description")

        when: 'tying to get the project by id'
            def sameProject = taigaClient.getProjectById(project.id)

        then: 'the projects are the same'
            project.id == sameProject.id
    }

    void 'save a project'() {
        when: 'saving a project'
            def project = taigaClient.createProject(name, description, Date.parse("dd/MM/yyyy", "01/01/2010"))

        then: 'the project is saved'
            project != null
            project.id != null
            project.name == name
            project.description == description
            project.issueStatuses.size() == 0
            project.issueTypes.size() == 0
            project.issuePriorities.size() == 0
            project.issueSeverities.size() == 0
            project.roles.size() == 0

        cleanup:
            taigaClient.deleteProject(project)

        where:
            name = "My project ${new Date().time}"
            description = 'The description of the project'
    }
}
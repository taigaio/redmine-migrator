package net.kaleidos.redmine.migrator

import spock.lang.Specification

import net.kaleidos.redmine.testdata.IssueDataProvider
import net.kaleidos.redmine.testdata.ProjectDataProvider

import net.kaleidos.redmine.RedmineTaigaRef
import net.kaleidos.redmine.MigratorToTaigaSpecBase
import net.kaleidos.redmine.RedmineFileDownloader
import net.kaleidos.redmine.testdata.ProjectDataProvider

import net.kaleidos.taiga.TaigaClient
import net.kaleidos.redmine.RedmineClient

import net.kaleidos.domain.Issue as TaigaIssue
import net.kaleidos.domain.History as TaigaHistory
import net.kaleidos.domain.Attachment as TaigaAttachment
import com.taskadapter.redmineapi.bean.Attachment as RedmineAttachment

class IssueMigratorSpec extends MigratorToTaigaSpecBase {

    @Delegate ProjectDataProvider projectDataProvider = new ProjectDataProvider()
    @Delegate IssueDataProvider issueDataProvider = new IssueDataProvider()

    void setup() {
        deleteTaigaProjects()
    }

    void 'migrating project issues'() {
        given: 'a mocked redmine client'
            RedmineClient redmineClient = buildRedmineClientToCreateProject()
            TaigaClient taigaClient = createTaigaClient()
        and: 'building a migrator instances'
            ProjectMigrator projectMigrator = new ProjectMigrator(redmineClient, taigaClient)
            IssueMigrator issueMigrator = new IssueMigrator(redmineClient, taigaClient)
        when: 'migrating a given project'
            RedmineTaigaRef migratedProjectInfo = projectMigrator.migrateProject(buildRedmineProject())
        and: 'trying to migrate basic the estructure of related issues'
            List<TaigaIssue> migratedIssues = issueMigrator.migrateIssuesByProject(migratedProjectInfo)
            TaigaIssue firstTaigaIssue = migratedIssues.first()
            TaigaAttachment firstIssueAttachment = firstTaigaIssue.attachments.first()
            TaigaHistory firstIssueHistory = firstTaigaIssue.history.first()
        then: 'checking basic request data'
            migratedIssues.size() > 0
            migratedIssues.every(basicData)
        and: 'checking first issue data in detail'
            firstTaigaIssue.type == 'tracker-1'
            firstTaigaIssue.status == 'status-1'
            firstTaigaIssue.priority == 'priority-1'
            firstTaigaIssue.severity == 'Normal'
            firstTaigaIssue.subject
            firstTaigaIssue.subject.contains('Integration')
            firstTaigaIssue.description
            firstTaigaIssue.owner
            firstTaigaIssue.assignedTo
            firstTaigaIssue.createdDate
        and: 'checking attachments'
            firstIssueAttachment.data
            firstIssueAttachment.name == "debian.jpg"
            firstIssueAttachment.owner
            firstIssueAttachment.description == "simple image"
            firstIssueAttachment.createdDate
        and: 'checking history'
            firstIssueHistory.user
            firstIssueHistory.createdAt
            firstIssueHistory.comment
            firstIssueHistory.comment.contains("some comment")
    }

    RedmineClient buildRedmineClientToCreateProject() {
        return Stub(RedmineClient) {
            findAllMembershipByProjectIdentifier(_) >> buildRedmineMembershipList()
            findUserFullById(_) >> { Integer id -> buildRedmineUser("${randomTime}") }
            findAllTracker() >> buildRedmineTrackerList()
            findAllIssueStatus() >> buildRedmineStatusList()
            findAllIssuePriority() >> buildRedmineIssuePriorityList()
            findAllIssueByProjectIdentifier(_) >> buildRedmineIssueList()
            findIssueById(_) >> { Integer index ->
                def redmineIssue = buildRedmineIssueWithIndex(index)
                redmineIssue.attachments = buildAttachmentList()
                redmineIssue.journals = buildHistoryList()
                redmineIssue
            }
            downloadAttachment(_) >> { RedmineAttachment att ->
                return new RedmineFileDownloader("http://lala","apiKey002ifj03ifj")
                    .download(att.contentURL)
            }
        }
    }

    void 'migrating project issues with no author or assignee'() {
        given: 'a mocked redmine client'
            RedmineClient redmineClient = buildRedmineClientWithIssuesWithoutOwners()
            TaigaClient taigaClient = createTaigaClient()
        and: 'building a migrator instances'
            ProjectMigrator projectMigrator = new ProjectMigrator(redmineClient, taigaClient)
            IssueMigrator issueMigrator = new IssueMigrator(redmineClient, taigaClient)
        when: 'migrating a given project'
            RedmineTaigaRef migratedProjectInfo = projectMigrator.migrateProject(buildRedmineProject())
        and: 'trying to migrate basic the estructure of related issues'
            List<TaigaIssue> migratedIssues = issueMigrator.migrateIssuesByProject(migratedProjectInfo)
            TaigaIssue firstTaigaIssue = migratedIssues.first()
            TaigaAttachment firstIssueAttachment = firstTaigaIssue.attachments.first()
            TaigaHistory firstIssueHistory = firstTaigaIssue.history.first()
        then: 'checking basic request data'
            migratedIssues.size() > 0
            migratedIssues.every(basicData)
    }

    RedmineClient buildRedmineClientWithIssuesWithoutOwners() {
        // Reusing general stub
        def stub = buildRedmineClientToCreateProject()

        // Modifying only how we're getting issues
        stub.findAllIssueByProjectIdentifier(_) >> {
            buildRedmineIssueList().collect {
                it.author= null
                it.assignee = null
                it
            }
        }

        return stub
    }

    void 'migrating project issue history only with comments'() {
        given: 'a mocked redmine client'
            RedmineClient redmineClient = buildRedmineClientToCreateProject()
            TaigaClient taigaClient = createTaigaClient()
        and: 'building a migrator instances'
            ProjectMigrator projectMigrator = new ProjectMigrator(redmineClient, taigaClient)
            IssueMigrator issueMigrator = new IssueMigrator(redmineClient, taigaClient)
        when: 'migrating a given project'
            RedmineTaigaRef migratedProjectInfo = projectMigrator.migrateProject(buildRedmineProject())
        and: 'trying to migrate basic the estructure of related issues'
            List<TaigaIssue> migratedIssues = issueMigrator.migrateIssuesByProject(migratedProjectInfo)
            TaigaIssue firstTaigaIssue = migratedIssues.first()
            TaigaAttachment firstIssueAttachment = firstTaigaIssue.attachments.first()
            TaigaHistory firstIssueHistory = firstTaigaIssue.history.first()
        then: 'checking basic request data'
            migratedIssues.size() > 0
            migratedIssues.every(basicData)
    }

    Closure<Boolean> basicData = {
        it.ref &&
        it.type &&
        it.status  &&
        it.priority  &&
        it.severity &&
        it.subject &&
        it.description &&
        it.project  &&
        it.owner  &&
        it.createdDate &&
        it.finishedDate &&
        it.attachments &&
        it.history
    }



}


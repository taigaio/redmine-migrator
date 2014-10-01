package gui.taiga

import gui.controller.*
import net.kaleidos.domain.Project

import org.viewaframework.annotation.*
import org.viewaframework.widget.view.*
import org.viewaframework.widget.view.ui.*
import org.viewaframework.view.DefaultViewContainer

@Controllers([
    @Controller(type=CloseViewController, pattern='closeTab')
])
class TaigaProjectListView extends MasterView<Project> {

    static final ID = 'taigaProjectListViewID'

    TaigaProjectListView() {
        super(ID, [
            new MasterViewColumn("name", 100),
            new MasterViewColumn("description", 100),
        ])
    }

    Class<Project> getMasterType() {
        return Project
    }

}

import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {Run} from "../../../../../../../site/src/app/domain/run";
import {Task} from "../../domain/task";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {ClassesStore} from "../../services/storage/classes-store";
import {TeacherService} from "../../../../../../../site/src/app/teacher/teacher.service";
import {TasksService} from "../../services/http/tasks.service";
import * as moment from "moment";

@Component({
  selector: 'app-run-datatable',
  templateUrl: './run-datatable.component.html',
  styleUrls: ['./run-datatable.component.scss']
})
export class RunDatatableComponent implements OnInit {

    runDataSource = new MatTableDataSource<Run>();
    runDisplayedColumns = ['id','name', 'startTime', 'endTime', 'numStudents', 'periods'];

    private runId: number;

    @ViewChild(MatSort, { static: true }) sortRuns: MatSort;
    @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

    private selectionTitle: string;
    private periodName: string;

    constructor(private classesStore: ClassesStore,
                private teacherService: TeacherService,
                private tasksService: TasksService) {
    }

    ngOnInit() {
        this.init();
    }

    init() {
        this.runDataSource.sort = this.sortRuns;
        this.refreshRunInformation();
    }

    resetAttributes() {
        this.runDataSource.sort = this.sortRuns;
    }

    refreshRunInformation() {
        this.runId = this.classesStore.runId;
        this.teacherService.getRun(this.runId)
            .subscribe(
                run => {
                    this.runDataSource.data.push(run);
                    this.resetAttributes();
                },
                err => console.log("Error retrieving run")
            );

    }

    convertTimestamp(timestamp: string) {
        if(timestamp == undefined)
            return ' ';
        return  moment(timestamp).format('MM/DD/YYYY HH:mm')
    }
}


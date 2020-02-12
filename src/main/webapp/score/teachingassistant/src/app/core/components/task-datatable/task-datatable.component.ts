import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {Run} from "../../../../../../../site/src/app/domain/run";
import {Task} from "../../domain/task";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {ClassesStore} from "../../services/storage/classes-store";
import {TeacherService} from "../../../../../../../site/src/app/teacher/teacher.service";
import {TasksService} from "../../services/http/tasks.service";
import * as moment from "moment";
import {MatSelectChange} from "@angular/material/select";
import {TaskRequest} from "../../domain/task-request";

@Component({
  selector: 'app-task-datatable',
  templateUrl: './task-datatable.component.html',
  styleUrls: ['./task-datatable.component.scss']
})
export class TaskDatatableComponent implements OnInit {

    tasksDataSource = new MatTableDataSource<Task>();
    tasksDisplayedColumns = ['id', 'name', 'workgroupId', 'workgroupName', 'periodId', 'duration', 'startTime', 'endTime', 'timeLeft', 'active', 'complete', 'requests'];
    periods: string[];

    @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
    @ViewChild(MatSort, { static: true }) sort: MatSort;

    private selectionTitle: string;
    private periodName: string;
    runId: number = 0;

    constructor(private classesStore: ClassesStore,
                private teacherService: TeacherService,
                private tasksService: TasksService) {
    }

    ngOnInit() {
        this.init();
    }

    init() {
        this.tasksDataSource.paginator = this.paginator;
        this.tasksDataSource.sort = this.sort;
        this.refreshRunInformation();
        this.refreshTasks();
    }

    resetAttributes() {
        this.tasksDataSource.paginator = this.paginator;
        this.tasksDataSource.sort = this.sort;
    }

    refreshRunInformation() {
        this.runId = this.classesStore.runId;
        this.teacherService.getRun(this.runId)
            .subscribe(
                run => {
                    this.periods = run.periods;
                    this.resetAttributes();
                },
                err => console.log("Error retrieving run")
            );

    }

    refreshTasks() {
        if (this.periodName) {
            this.tasksService.getTasksByRunIdAndPeriodName(this.runId, this.periodName).subscribe(tasks => {
                this.tasksDataSource.data = [];
                for (let i = 0; i < tasks.length; i++) {
                    let task: Task = tasks[i];
                    if (task.complete == false) {
                        this.tasksDataSource.data.filter(function (element) {
                            return element.id != task.id;
                        });
                    }
                    this.tasksDataSource.data.push(task);
                    this.resetAttributes();
                }

            });
        }
    }

    convertTimestamp(timestamp: string) {
        if (timestamp == undefined)
            return ' ';
        return moment(timestamp).format('MM/DD/YYYY HH:mm')
    }

    periodSelectionChange($event: MatSelectChange) {
        this.periodName = $event.value;
        this.selectionTitle = `for Period ${this.periodName}`;
        this.refreshTasks();

    }

    findTask(taskRequests: TaskRequest[]): string {
        for (let i = 0; i < taskRequests.length; i++) {
            let taskRequest: TaskRequest = taskRequests[i];
            if (taskRequest.complete == false) {
                return taskRequest.status;
            }
        }
        return 'none';
    }

    calculateTimeLeft(task: Task) {

    }

    taskRequestCompleteAction(task: Task) {
        console.log('Task', task);
    }
}

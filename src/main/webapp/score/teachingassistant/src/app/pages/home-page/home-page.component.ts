import {MatTableDataSource} from "@angular/material/table";
import {Component, OnInit, ViewChild} from "@angular/core";
import {Run} from "../../../../../../site/src/app/domain/run";
import {MatSort} from "@angular/material/sort";
import {ClassesStore} from "../../core/services/storage/classes-store";
import {TeacherService} from "../../../../../../site/src/app/teacher/teacher.service";
import * as moment from "moment";
import {MatSelectChange} from "@angular/material/select";
import {TasksService} from "../../core/services/http/tasks.service";
import { Task } from 'src/app/core/domain/task';
import {TaskRequest} from "../../core/domain/task-request";
import {MatPaginator} from "@angular/material/paginator";

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent implements OnInit {

    runDataSource = new MatTableDataSource<Run>();
    tasksDataSource = new MatTableDataSource<Task>();
    runDisplayedColumns = ['id','name', 'startTime', 'endTime', 'numStudents', 'periods'];
    tasksDisplayedColumns = ['id','name','workgroupId', 'workgroupName', 'periodId','duration', 'startTime', 'endTime', 'timeLeft','complete','requests'];
    periods: string[];

    @ViewChild(MatSort, { static: true }) sortTasks: MatSort;
    @ViewChild(MatSort, { static: true }) sortRuns: MatSort;
    @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

    private selectionTitle: string;
    private periodName: string;
    private runId: number;

    constructor(private classesStore: ClassesStore,
                private teacherService: TeacherService,
                private tasksService: TasksService) {
    }

    ngOnInit() {
        this.init();
    }

    init() {
        this.tasksDataSource.paginator = this.paginator;
        this.tasksDataSource.sort = this.sortTasks;
        this.runDataSource.sort = this.sortRuns;
        this.refreshRunInformation();
        this.refreshTasks();
    }

    resetAttributes() {
        this.tasksDataSource.paginator = this.paginator;
        this.runDataSource.sort = this.sortRuns;
        this.tasksDataSource.sort = this.sortTasks;
    }

    refreshRunInformation() {
        this.runId = this.classesStore.runId;
        this.teacherService.getRun(this.runId)
            .subscribe(
                run => {
                    this.runDataSource.data.push(run);
                    this.periods = run.periods;
                    this.resetAttributes();
                },
                err => console.log("Error retrieving run")
            );

    }
    refreshTasks() {
        if(this.periodName) {
            this.tasksService.getTasksByRunIdAndPeriodName(this.runId, this.periodName).subscribe(tasks => {
                this.tasksDataSource.data =[];
                for (let i = 0; i < tasks.length; i++) {
                    let task: Task = tasks[i];
                    this.tasksDataSource.data.filter(function (element) {
                        return element.id != task.id;
                    });
                    this.tasksDataSource.data.push(task);
                }
                this.resetAttributes();
            });
        }
    }

    convertTimestamp(timestamp: string) {
        if(timestamp == undefined)
            return ' ';
        return  moment(timestamp).format('MM/DD/YYYY HH:mm')
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
}

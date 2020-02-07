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

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent implements OnInit {

    runDataSource = new MatTableDataSource<Run>();
    tasksDataSource = new MatTableDataSource<Task>();
    runDisplayedColumns = ['id','name', 'startTime', 'endTime', 'numStudents', 'periods'];
    tasksDisplayedColumns = ['id','workgroupId', 'workgroupName', 'periodId', 'complete','requests'];
    periodTitle: string;
    periods: string[];

    @ViewChild(MatSort, { static: true }) sort: MatSort;
    private periodId: number;
    private runId: number;

    constructor(private classesStore: ClassesStore,
                private teacherService: TeacherService,
                private tasksService: TasksService) {
    }

    ngOnInit() {
        this.init();
    }

    init() {
        this.runDataSource.sort = this.sort;
        this.tasksDataSource.sort = this.sort;
        this.refreshRunInformation();
        this.refreshTasks();
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
        if(this.periodId) {
            this.tasksService.getTasksByRunIdAndPeriodId(this.runId, this.periodId).subscribe(tasks => {
                this.tasksDataSource.data =[];
                for (let i = 0; i < tasks.length; i++) {
                    let task: Task = tasks[i];
                    this.tasksDataSource.data.filter(function (element) {
                        return element.id != task.id;
                    });
                    console.log('TASK REWUEST',task.taskRequests);
                    this.tasksDataSource.data.push(task);
                    this.resetAttributes();
                }
            });
        }
    }

    resetAttributes() {
        this.runDataSource.sort = this.sort;
        this.tasksDataSource.sort = this.sort;
    }

    convertTimestamp(timestamp: string) {
        return  moment(timestamp).format('MM/DD/YYYY HH:mm')
    }

    periodSelectionChange($event: MatSelectChange) {
        this.periodId = $event.value;
        this.periodTitle = `for Period ${this.periodId}`;
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
}

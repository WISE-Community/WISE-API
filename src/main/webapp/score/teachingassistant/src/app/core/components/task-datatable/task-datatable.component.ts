import { Component, OnInit, ViewChild, Output } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Run } from '../../../../../../../site/src/app/domain/run';
import { Task } from '../../domain/task';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { TeacherService } from '../../../../../../../site/src/app/teacher/teacher.service';
import { TasksService } from '../../services/http/tasks.service';
import * as moment from 'moment';
import { MatSelectChange } from '@angular/material/select';
import { TaskRequest } from '../../domain/task-request';
import { Period } from '../../../../../../../site/src/app/domain/period';
import { WebSocketService } from '../../services/websocket/websocket.service';
import { UpgradeModule } from '@angular/upgrade/static';

@Component({
    selector: 'app-task-datatable',
    templateUrl: './task-datatable.component.html',
    styleUrls: ['./task-datatable.component.scss'],
})
export class TaskDatatableComponent implements OnInit {
    tasksDataSource = new MatTableDataSource<Task>();
    tasksDisplayedColumns = [
        'workgroupId',
        'name',
        'timeLeft',
        'requests',
    ];
    periods: Period[];

    @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
    @ViewChild(MatSort, { static: true }) sort: MatSort;

    selectionTitle: string;
    periodName: string;
    runId: number = 0;

    constructor(
        private upgrade: UpgradeModule,
        private teacherService: TeacherService,
        private tasksService: TasksService,
        private websocketService: WebSocketService,
    ) { }

    ngOnInit() {
        this.websocketService._connect();
        this.tasksDataSource.paginator = this.paginator;
        this.tasksDataSource.sort = this.sort;
        this.periodName = this.upgrade.$injector.get("TeacherDataService").getCurrentPeriod().periodName;
        this.upgrade.$injector.get('$rootScope').$on('currentPeriodChanged', (event, args) => {
            this.periodName = args.currentPeriod.periodName;
            this.refreshTasks();
        });
        this.refreshRunInformation();
        this.refreshTasks();
        setInterval(() => {
            this.refreshTasks();
        }, 5000);
    }

    resetAttributes() {
        this.tasksDataSource.paginator = this.paginator;
        this.tasksDataSource.sort = this.sort;
    }

    refreshRunInformation() {
        this.runId = this.upgrade.$injector.get("ConfigService").getRunId();
        this.teacherService.getRun(this.runId).subscribe(
            run => {
                this.periods = run.periods;
                this.resetAttributes();
            },
            err => console.log('Error retrieving run'),
        );
    }

    refreshTasks() {
        if (this.periodName) {
            this.tasksService
                .getTasksByRunIdAndPeriodName(this.runId, this.periodName)
                .subscribe(tasks => {
                    this.tasksDataSource.data = [];
                    for (let i = 0; i < tasks.length; i++) {
                        let task: Task = tasks[i];
                        if (task.complete == false) {
                            this.tasksDataSource.data.filter(function (element) {
                                return element.id != task.id;
                            });
                        }
                        if (task.active) {
                            this.tasksDataSource.data.push(task);
                        }
                        this.resetAttributes();
                    }
                });
        }
    }

    convertTimestamp(timestamp: string) {
        if (timestamp == undefined) return ' ';
        return moment(timestamp).format('MM/DD/YYYY HH:mm');
    }

    periodSelectionChange($event: MatSelectChange) {
        this.periodName = $event.value;
        this.selectionTitle = `for Period ${this.periodName}`;
        this.refreshTasks();
    }

    findTask(taskRequests: TaskRequest[]): string {
        for (let i = 0; i < taskRequests.length; i++) {
            let taskRequest: TaskRequest = taskRequests[i];
            console.log('taskRequest ', taskRequest);
            if (taskRequest.complete == false) {
                return taskRequest.status;
            }
        }
        return 'none';
    }

    calculateTimeLeft(task: Task) {
        if (task.endTime) {
            let now = moment();
            let end = moment(task.endTime);

            var duration = end.diff(now);
            // console.log('DIFFF', duration);
        }

        return 0;
    }

    taskRequestCompleteAction(taskRequest: TaskRequest, status: string) {
        this.tasksService
            .completeTaskRequest(taskRequest.id, status)
            .subscribe(tr => {
                if (status === 'approved') {
                    this.sendRequestApprovedMessageToStudent(tr);
                }
                this.refreshTasks();
                console.log('Task Request', taskRequest);
            });
    }

    sendRequestApprovedMessageToStudent(taskRequest: TaskRequest) {
        this.websocketService._send(
            `/app/api/teacher/run/${taskRequest.runId}/workgroup-to-next-node/${taskRequest.workgroupId}`,
            null,
        );
    }
}

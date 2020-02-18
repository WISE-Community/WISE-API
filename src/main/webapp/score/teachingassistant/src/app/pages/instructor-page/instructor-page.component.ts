import { Component, OnInit } from '@angular/core';
import { TeacherService } from '../../../../../../site/src/app/teacher/teacher.service';
import { WebSocketService } from 'src/app/core/services/websocket/websocket.service';
import { ClassesStore } from 'src/app/core/services/storage/classes-store';
import { Run } from '../../../../../../site/src/app/domain/run';
import { Workgroup } from '../../../../../../site/src/app/domain/workgroup';

@Component({
    selector: 'app-instructor-page',
    templateUrl: './instructor-page.component.html',
    styleUrls: ['./instructor-page.component.scss'],
})
export class InstructorPageComponent implements OnInit {
    private run: Run;
    private workgroups: Workgroup[];
    displayedColumns: string[] = ['id', 'name', 'currentNode', 'actions'];

    constructor(
        private classesStore: ClassesStore,
        private teacherService: TeacherService,
        private websocketService: WebSocketService,
    ) {}

    ngOnInit() {
        this.initIoConnection();
        this.run = this.classesStore.run;
        this.teacherService.getRun(this.classesStore.runId).subscribe(
            run => {
                this.run = run;
                this.getWorkgroups(run);
            },
            err => console.log('Error retrieving run'),
        );
    }

    getWorkgroups(run: Run) {
        this.teacherService.getWorkgroups(run).subscribe(workgroups => {
            this.workgroups = workgroups;
        })
    }

    sendWorkgroupToNode(workgroupId = 390843, nodeId: string = 'node1') {
        this.websocketService._send(
            `/app/api/teacher/run/${this.run.id}/workgroup-to-node/${workgroupId}`,
            nodeId
        );
    }

    sendPeriodToNode(periodId: number = 428439, nodeId: string = 'node1') {
        this.websocketService._send(
            `/app/api/teacher/run/${this.run.id}/period-to-node/${periodId}`,
            nodeId
        );
    }

    pauseAllScreens(periodId: number = 428439) {
        this.websocketService._send(
            `/app/pause/${this.run.id}/${periodId}`, ''
        );
    }

    unpauseAllScreens(periodId: number = 428439) {
        this.websocketService._send(
            `/app/unpause/${this.run.id}/${periodId}`, ''
        );
    }

    private initIoConnection(): void {
        this.websocketService._connect();
        /*
        this.ioConnection = this.socketService
            .onMessage()
            .subscribe((message: Message) => {
                this.messages.push(message);
            });

        this.socketService.onEvent(Event.CONNECT).subscribe(() => {
            console.log('connected');
        });

        this.socketService.onEvent(Event.DISCONNECT).subscribe(() => {
            console.log('disconnected');
        });
        */
    }
}

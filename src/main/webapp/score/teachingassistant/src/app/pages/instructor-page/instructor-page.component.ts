import { Component, OnInit } from '@angular/core';
import { TeacherService } from '../../../../../../site/src/app/teacher/teacher.service';
import { WebSocketService } from '../../core/services/websocket/websocket.service';
import { ClassesStore } from '../../core/services/storage/classes-store';
import { Run } from '../../../../../../site/src/app/domain/run';
import { Workgroup } from '../../../../../../site/src/app/domain/workgroup';
import { MatDialog } from '@angular/material/dialog';
import { GoToNodeSelectComponent } from '../../core/components/go-to-node-select/go-to-node-select.component';
import { Period } from '../../../../../../site/src/app/domain/period';
import { MatTableDataSource } from '@angular/material/table';

@Component({
    selector: 'app-instructor-page',
    templateUrl: './instructor-page.component.html',
    styleUrls: ['./instructor-page.component.scss'],
})
export class InstructorPageComponent implements OnInit {
    private run: Run;
    private allWorkgroupsInRun: Workgroup[];
    private workgroups: any = {};
    dataSource = new MatTableDataSource<Run>();
    displayedColumns: string[] = ['id', 'name', 'actions'];

    constructor(
        private dialog: MatDialog,
        private classesStore: ClassesStore,
        private teacherService: TeacherService,
        private websocketService: WebSocketService,
    ) {}

    ngOnInit() {
        this.initIoConnection();
        this.run = this.classesStore.run;
        this.teacherService.getRun(303).subscribe(
            (run) => {
                this.run = new Run(run);
                this.getWorkgroups(this.run);
                this.getProjectContent(this.run);
            },
            (err) => console.log('Error retrieving run'),
        );
    }

    getWorkgroupsInPeriod(period: Period) {
        return this.workgroups[period.id];
    }

    getWorkgroups(run: Run) {
        this.teacherService
            .getWorkgroups(run)
            .subscribe((allWorkgroupsInRun) => {
                this.allWorkgroupsInRun = allWorkgroupsInRun.filter(
                    (workgroupInRun) => {
                        return workgroupInRun.isStudentWorkgroup;
                    },
                );
                this.putWorkgroupsInPeriod();
            });
    }

    putWorkgroupsInPeriod() {
        for (const workgroup of this.allWorkgroupsInRun) {
            this.putWorkgroupInPeriod(workgroup);
        }
    }

    putWorkgroupInPeriod(workgroup) {
        for (const period of this.run.periods) {
            if (period.id === workgroup.period.id) {
                period.workgroups.push(workgroup);
            }
        }
    }

    getProjectContent(run: Run) {
        this.teacherService.getProjectContent(run.project);
    }

    chooseNodeToSendWorkgroup(workgroup: Workgroup) {
        this.dialog.open(GoToNodeSelectComponent, {
            minWidth: '600px',
            maxHeight: '800px',
            data: { workgroup: workgroup, run: this.run },
            panelClass: 'mat-dialog--md',
        });
    }

    chooseNodeToSendPeriod(period: Period) {
        this.dialog.open(GoToNodeSelectComponent, {
            minWidth: '600px',
            maxHeight: '800px',
            data: { period: period, run: this.run },
            panelClass: 'mat-dialog--md',
        });
    }

    pauseAllScreens(period: Period) {
        this.websocketService._send(
            `/app/pause/${this.run.id}/${period.id}`,
            '',
        );
    }

    unpauseAllScreens(period: Period) {
        this.websocketService._send(
            `/app/unpause/${this.run.id}/${period.id}`,
            '',
        );
    }

    private initIoConnection(): void {
        this.websocketService._connect();
    }
}

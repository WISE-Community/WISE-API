import {MatTableDataSource} from "@angular/material/table";
import {Component, OnInit, ViewChild} from "@angular/core";
import {Run} from "../../../../../../site/src/app/domain/run";
import {MatSort} from "@angular/material/sort";
import {ClassesStore} from "../../core/services/storage/classes-store";
import {TeacherService} from "../../../../../../site/src/app/teacher/teacher.service";
import * as moment from "moment";
import {MatSelectChange} from "@angular/material/select";

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent implements OnInit {

    runDataSource = new MatTableDataSource<Run>();
    tasksDataSource = new MatTableDataSource<any>();
    runDisplayedColumns = ['id','name', 'startTime', 'endTime', 'numStudents', 'periods'];
    tasksDisplayedColumns = ['id','name', 'startTime', 'endTime', 'numStudents', 'periods', 'project'];
    periodTitle: string;
    periods: string[];

    @ViewChild(MatSort, { static: true }) sort: MatSort;
    private selectedPeriod: Number;

    constructor(private classesStore: ClassesStore,
                private teacherService: TeacherService) {
    }

    ngOnInit() {
        this.init();
    }

    init() {
        this.runDataSource.sort = this.sort;
        this.refreshRunInformation();
        this.refreshTasks();
    }

    refreshRunInformation() {
        let runId = this.classesStore.runId;
        this.teacherService.getRun(runId)
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
        if(this.selectedPeriod) {

        }
    }

    resetAttributes() {
        this.runDataSource.sort = this.sort;
    }

    convertTimestamp(timestamp: string) {
        return  moment(timestamp).format('MM/DD/YYYY HH:mm')
    }

    periodSelectionChange($event: MatSelectChange) {
        this.selectedPeriod = $event.value;
        this.periodTitle = `for ${this.selectedPeriod}`;

        console.log(this.selectedPeriod);

    }
}

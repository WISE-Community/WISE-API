import { MatTableDataSource } from "@angular/material/table";
import { Component, OnInit, ViewChild } from "@angular/core";
import { Run } from "../../../../../../site/src/app/domain/run";
import { MatSort } from "@angular/material/sort";
import { ClassesStore } from "../../core/services/storage/classes-store";
import { TeacherService } from "../../../../../../site/src/app/teacher/teacher.service";
import * as moment from "moment";
import { MatSelectChange } from "@angular/material/select";
import { MatPaginator } from "@angular/material/paginator";
import { Period } from '../../../../../../site/src/app/domain/period';

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent implements OnInit {

    constructor() {
    }

    ngOnInit() {
    }

    init() {
    }

}

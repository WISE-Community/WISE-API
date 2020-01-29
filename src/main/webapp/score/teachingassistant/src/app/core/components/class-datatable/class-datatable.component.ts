import {Component, OnInit, ViewChild} from '@angular/core';
import {GroupStatusDetail} from '../../domain/group-status-detail';
import {MatSort, MatTableDataSource} from '@angular/material';

@Component({
    selector: 'app-class-datatable',
    templateUrl: './class-datatable.component.html',
    styleUrls: ['./class-datatable.component.scss'],
})
export class ClassDatatableComponent implements OnInit {
    dataSource = new MatTableDataSource<GroupStatusDetail>();
    displayedColumns = ['groupId', 'title', 'time', 'status'];

    @ViewChild(MatSort, { static: true }) sort: MatSort;

    constructor() {}

    ngOnInit() {
        this.init();
    }

    init() {
        this.dataSource.sort = this.sort;
        this.refresh();
    }

    refresh() {
    }

    resetAttributes() {
        this.dataSource.sort = this.sort;
    }
}

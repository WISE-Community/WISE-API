import {Component} from '@angular/core';
import {ActivatedRoute, Router, RouterEvent} from "@angular/router";
import {ConfigService} from "../../../../site/src/app/services/config.service";
import {TeacherService} from "../../../../site/src/app/teacher/teacher.service";
import {TeacherRun} from "../../../../site/src/app/teacher/teacher-run";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent {
    title = 'SCORE TA Monitoring App';
    private contextPath: string;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private teacherService: TeacherService,
    ) {

        // console.log('CONFIG',this.configService.getContextPath());
        // if(window.location.href) {
        //     let split = window.location.href.split('/');
        //     let runId = split[split.length-1];
        //     console.log('runId: ', runId);
            this.teacherService.getRun(Number(7)).subscribe(runs => {

                console.log('runs', runs);
            });
        // }
    }

    ngOnInit() {
    }
}

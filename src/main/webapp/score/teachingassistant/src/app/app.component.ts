import {Component} from '@angular/core';
import {ActivatedRoute, Router, RouterEvent} from "@angular/router";
import {ConfigService} from "../../../../site/src/app/services/config.service";
import {TeacherService} from "../../../../site/src/app/teacher/teacher.service";
import {TeacherRun} from "../../../../site/src/app/teacher/teacher-run";
import {Observable} from "rxjs";
import {Run} from "../../../../site/src/app/domain/run";
import {ClassesStore} from "./core/services/storage/classes-store";

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
        private classesStore: ClassesStore,
    ) {
<<<<<<< HEAD

        // console.log('CONFIG',this.configService.getContextPath());
        // if(window.location.href) {
        //     let split = window.location.href.split('/');
        //     let runId = split[split.length-1];
        //     console.log('runId: ', runId);
            this.teacherService.getRun(Number(7)).subscribe(runs => {

                console.log('runs', runs);
            });
        // }
=======
        console.log(window.location.href);
        if(window.location.href) {
            let split = window.location.href.split('/');
            let runId = split[split.length-1];
            console.log('runId: ', runId);

            this.classesStore.runId = Number(runId);
            // console.log('RUN ----', this.classesStore.run)

            //find the run by id
            //iterate periods
            //for each period grabs all the tasks
            //call http://localhost:8080/api/tasks/{periodId)

            // /api/tasks
            // baseURl + /api/tasks

        }
>>>>>>> 5ce24adc4... overview fetching runs and tasks
    }

    ngOnInit() {
    }

    //service
    // getTaskByPeriod(periodID: number): Observable<Task> {
    //     return this.http.get<Run>(`${this.}/${runId}`);
    // }
}

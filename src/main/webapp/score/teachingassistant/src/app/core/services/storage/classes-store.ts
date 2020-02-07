import {TeacherService} from "../../../../../../../site/src/app/teacher/teacher.service";
import {Run} from "../../../../../../../site/src/app/domain/run";
import {Injectable} from "@angular/core";
@Injectable({
    providedIn: 'root'
})
export class ClassesStore {

    private _run: Run;
    private _runId: number;

    constructor(private teacherService: TeacherService) {

    }

    loadInitialData(runId: number) {
        this.teacherService.getRun(runId)
            .subscribe(
                run => {
                    this._run = run;
                    // console.log('runs ----', run);
                },
                err => console.log("Error retrieving run")
            );

    }

    get runId(): number {
        return this._runId;
    }

    set runId(value: number) {
        this._runId = value;
    }

    get run(): Run {
        return this._run;
    }

    set run(value: Run) {
        this._run = value;
    }
}

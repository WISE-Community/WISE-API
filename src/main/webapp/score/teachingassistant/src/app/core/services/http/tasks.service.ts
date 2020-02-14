import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {Task} from '../../domain/task';

@Injectable({
    providedIn: 'root'
})
export class TasksService {
    private tasksUrl = '/api/tasks';

    constructor(private http: HttpClient) {
    }

    startTaskTimer(workgroupId: string, activityId: string, runId: number): any {
        console.log('start timer called');
        const headers = new HttpHeaders({'Cache-Control': 'no-cache'});
        this.http.get<any>(`${this.tasksUrl}/start/${runId}/${workgroupId}/${activityId}`, {headers: headers});
    }

    stopTaskTimer(workgroupId: string, activityId: string, runId: number): any {
        console.log('stop timer called');
        const headers = new HttpHeaders({'Cache-Control': 'no-cache'});
        this.http.get<any>(`${this.tasksUrl}/stop/${runId}/${workgroupId}/${activityId}`, {headers: headers});
    }

    completeTaskRequest(taskRequestId: string, status: string): Observable<any> {
        const headers = new HttpHeaders({'Cache-Control': 'no-cache'});
        return this.http.get<any>(`${this.tasksUrl}/taskrequest/${taskRequestId}/${status}`, {headers: headers});
    }

    getTasksByRunIdAndPeriodId(runId: number, periodId: number): Observable<Task[]> {
        const headers = new HttpHeaders({'Cache-Control': 'no-cache'});
        return this.http.get<Task[]>(`${this.tasksUrl}/id/${runId}/${periodId}`, {headers: headers});
    }

    getTasksByRunIdAndPeriodName(runId: number, periodName: string): Observable<Task[]> {
        const headers = new HttpHeaders({'Cache-Control': 'no-cache'});
        return this.http.get<Task[]>(`${this.tasksUrl}/name/${runId}/${periodName}`, {headers: headers});
    }

    getTasks(): Observable<Task[]> {
        const headers = new HttpHeaders({'Cache-Control': 'no-cache'});
        return this.http.get<Task[]>(this.tasksUrl, {headers: headers});
    }
}

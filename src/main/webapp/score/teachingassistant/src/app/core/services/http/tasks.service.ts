import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import { Task } from '../../domain/task';

@Injectable({
    providedIn: 'root'
})
export class TasksService {
    private tasksUrl = '/api/tasks';

    constructor(private http: HttpClient) {
    }

    getTasksByRunIdAndPeriodId(runId: number, periodId: number): Observable<Task[]> {
        const headers = new HttpHeaders({ 'Cache-Control': 'no-cache' });
        return this.http.get<Task[]>(`${this.tasksUrl}/${runId}/${periodId}`, { headers: headers });
    }

    getTasks(): Observable<Task[]> {
        const headers = new HttpHeaders({ 'Cache-Control': 'no-cache' });
        return this.http.get<Task[]>(this.tasksUrl, { headers: headers });
    }
}

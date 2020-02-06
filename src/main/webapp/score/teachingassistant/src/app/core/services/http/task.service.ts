import {Injectable} from '@angular/core';
import {CrudService} from "../http/crud.service";
import {HttpClient} from "@angular/common/http";

@Injectable({
    providedIn: 'root'
})
export class TaskService extends CrudService {

    endpoint = 'api/tasks';
    url = 'http://localhost:8080'

    constructor(http: HttpClient) {
        super(http);
    }

    public async getAllTasks(): Promise<any> {
        return this.get('');
    }

}

import {TaskRequest} from "./task-request";

export class Task {
    constructor(
        public id?: string,
        public name?: string,
        public workgroupId?: number,
        public runId?: number,
        public workgroupName?: string,
        public projectId?: number,
        public startTime?: string,
        public endTime?: string,
        public periodName?:string,
        public periodId?: number,
        public started?: boolean,
        public complete?: boolean,
        public taskRequests?: TaskRequest[],
        public duration?: number,
    ) {}
}

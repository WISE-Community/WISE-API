export class TaskRequest {
    constructor(
        public id?: string,
        public name?: string,
        public workgroupId?: number,
        public runId?: number,
        public projectId?: number,
        public periodId?: number,
        public startTime?: string,
        public endTime?: string,
        public status?: string,
        public complete?: boolean,
    ) {}
}

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { TaskService } from './task.service';

describe('TaskService', () => {
    beforeEach(() =>
        TestBed.configureTestingModule({ imports: [HttpClientTestingModule] }),
    );

    it('should be created', () => {
        const service: TaskService = TestBed.get(TaskService);
        expect(service).toBeTruthy();
    });
});

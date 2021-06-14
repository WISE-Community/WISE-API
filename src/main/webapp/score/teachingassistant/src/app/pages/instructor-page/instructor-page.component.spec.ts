import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { TeacherService } from '../../../../../../site/src/app/teacher/teacher.service';

import { InstructorPageComponent } from './instructor-page.component';

class MockTeacherService {
    getRun() {}
}

describe('InstructorPageComponent', () => {
    let component: InstructorPageComponent;
    let fixture: ComponentFixture<InstructorPageComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [MatDialogModule],
            providers: [
                { provide: TeacherService, useClass: MockTeacherService },
            ],
            declarations: [InstructorPageComponent],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(InstructorPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });
});

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { GoToNodeSelectComponent } from './go-to-node-select.component';

describe('GoToNodeSelectComponent', () => {
    let component: GoToNodeSelectComponent;
    let fixture: ComponentFixture<GoToNodeSelectComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [MatDialogModule],
            providers: [
                {
                    provide: MAT_DIALOG_DATA,
                    useValue: {
                        run: { project: { idToOrder: { nodes: [] } } },
                    },
                },
            ],
            declarations: [GoToNodeSelectComponent],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GoToNodeSelectComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

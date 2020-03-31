import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GoToNodeSelectComponent } from './go-to-node-select.component';

describe('GoToNodeSelectComponent', () => {
  let component: GoToNodeSelectComponent;
  let fixture: ComponentFixture<GoToNodeSelectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GoToNodeSelectComponent ]
    })
    .compileComponents();
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

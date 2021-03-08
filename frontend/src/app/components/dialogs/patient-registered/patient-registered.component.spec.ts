import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientRegisteredComponent } from './patient-registered.component';

describe('DialogComponent', () => {
  let component: PatientRegisteredComponent;
  let fixture: ComponentFixture<PatientRegisteredComponent>;

  /* tslint:disable:deprecation */ // TODO
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PatientRegisteredComponent]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientRegisteredComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

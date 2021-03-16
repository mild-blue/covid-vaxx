import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientQuestionsFormComponent } from './patient-questions-form.component';

describe('PatientQuestionsFormComponent', () => {
  let component: PatientQuestionsFormComponent;
  let fixture: ComponentFixture<PatientQuestionsFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PatientQuestionsFormComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientQuestionsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

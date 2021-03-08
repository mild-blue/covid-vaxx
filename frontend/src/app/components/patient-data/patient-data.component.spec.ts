import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientDataComponent } from './patient-data.component';

describe('PatientDataComponent', () => {
  let component: PatientDataComponent;
  let fixture: ComponentFixture<PatientDataComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PatientDataComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

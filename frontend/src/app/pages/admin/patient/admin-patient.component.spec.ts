import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminPatientComponent } from './admin-patient.component';

describe('PatientDetailComponent', () => {
  let component: AdminPatientComponent;
  let fixture: ComponentFixture<AdminPatientComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminPatientComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminPatientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

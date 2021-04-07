import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfirmPatientDataComponent } from './confirm-patient-data.component';

describe('ConfirmPatientDataComponent', () => {
  let component: ConfirmPatientDataComponent;
  let fixture: ComponentFixture<ConfirmPatientDataComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConfirmPatientDataComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmPatientDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

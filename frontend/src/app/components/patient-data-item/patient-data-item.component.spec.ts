import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientDataItemComponent } from './patient-data-item.component';

describe('FormFieldComponent', () => {
  let component: PatientDataItemComponent;
  let fixture: ComponentFixture<PatientDataItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PatientDataItemComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientDataItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

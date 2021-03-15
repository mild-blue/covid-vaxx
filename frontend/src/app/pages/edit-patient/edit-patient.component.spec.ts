import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditPatientComponent } from './edit-patient.component';

describe('EditPatientComponent', () => {
  let component: EditPatientComponent;
  let fixture: ComponentFixture<EditPatientComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditPatientComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditPatientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

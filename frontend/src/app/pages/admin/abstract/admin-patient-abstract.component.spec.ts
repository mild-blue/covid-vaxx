import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminPatientAbstractComponent } from './admin-patient-abstract.component';

describe('AbstractComponent', () => {
  let component: AdminPatientAbstractComponent;
  let fixture: ComponentFixture<AdminPatientAbstractComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminPatientAbstractComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminPatientAbstractComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoPatientFoundComponent } from './no-patient-found.component';

describe('NoPatientFoundComponent', () => {
  let component: NoPatientFoundComponent;
  let fixture: ComponentFixture<NoPatientFoundComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NoPatientFoundComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NoPatientFoundComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

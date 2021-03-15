import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfirmVaccinationComponent } from './confirm-vaccination.component';

describe('ConfirmVaccinationComponent', () => {
  let component: ConfirmVaccinationComponent;
  let fixture: ComponentFixture<ConfirmVaccinationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConfirmVaccinationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmVaccinationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

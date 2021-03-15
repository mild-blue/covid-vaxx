import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminDetailComponent } from './admin-detail.component';

describe('PatientDetailComponent', () => {
  let component: AdminDetailComponent;
  let fixture: ComponentFixture<AdminDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminDetailComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

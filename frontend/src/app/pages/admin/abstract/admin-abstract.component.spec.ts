import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminAbstractComponent } from './admin-abstract.component';

describe('AbstractComponent', () => {
  let component: AdminAbstractComponent;
  let fixture: ComponentFixture<AdminAbstractComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminAbstractComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminAbstractComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

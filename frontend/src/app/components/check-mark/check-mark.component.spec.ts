import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckMarkComponent } from './check-mark.component';

describe('CheckMarkComponent', () => {
  let component: CheckMarkComponent;
  let fixture: ComponentFixture<CheckMarkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CheckMarkComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckMarkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AbstractConfirmComponent } from './abstract-confirm.component';

describe('AbstractConfirmComponent', () => {
  let component: AbstractConfirmComponent;
  let fixture: ComponentFixture<AbstractConfirmComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AbstractConfirmComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AbstractConfirmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

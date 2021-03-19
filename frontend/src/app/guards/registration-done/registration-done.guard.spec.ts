import { TestBed } from '@angular/core/testing';

import { RegistrationDoneGuard } from './registration-done.guard';

describe('RegistrationDoneGuard', () => {
  let guard: RegistrationDoneGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(RegistrationDoneGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});

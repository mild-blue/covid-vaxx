import { TestBed } from '@angular/core/testing';

import { InsuranceService } from './insurance.service';

describe('InsuranceService', () => {
  let service: InsuranceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InsuranceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

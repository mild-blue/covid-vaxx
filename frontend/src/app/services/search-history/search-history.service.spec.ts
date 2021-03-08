import { TestBed } from '@angular/core/testing';

import { SearchHistoryService } from './search-history.service';

describe('SearchHistoryService', () => {
  let service: SearchHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SearchHistoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

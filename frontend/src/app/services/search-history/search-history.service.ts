import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SearchHistoryService {

  // eslint-disable-next-line no-magic-numbers
  private _searchHistoryLimit = 5;
  private _searchHistoryKey: string = 'searchHistory';

  get searchHistory(): { search: string; isForeigner: boolean; }[] {
    const value = localStorage.getItem(this._searchHistoryKey);
    const searches: string[] = value ? JSON.parse(value) : [];
    return searches.map(data => JSON.parse(data));
  }

  public saveSearch(search: string): void {
    const data = JSON.stringify({ search });
    const storageValues = localStorage.getItem(this._searchHistoryKey);

    if (!storageValues) {
      localStorage.setItem(this._searchHistoryKey, JSON.stringify([data]));
      return;
    }

    let searchHistory = JSON.parse(storageValues) as string[];
    searchHistory.unshift(data);
    searchHistory = searchHistory.slice(0, this._searchHistoryLimit);

    localStorage.setItem(this._searchHistoryKey, JSON.stringify(searchHistory));
  }

  public clearHistory(): void {
    localStorage.removeItem(this._searchHistoryKey);
  }
}

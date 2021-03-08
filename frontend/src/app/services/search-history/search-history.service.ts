import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SearchHistoryService {

  // eslint-disable-next-line no-magic-numbers
  private _searchHistoryLimit = 5;
  private _searchHistoryKey: string = 'searchHistory';

  get searchHistory(): string[] {
    const value = localStorage.getItem(this._searchHistoryKey);
    return value ? JSON.parse(value) : [];
  }

  public saveSearch(search: string): void {
    const storageValues = localStorage.getItem(this._searchHistoryKey);

    if (!storageValues) {
      localStorage.setItem(this._searchHistoryKey, JSON.stringify([search]));
      return;
    }

    let searchHistory = JSON.parse(storageValues) as string[];
    searchHistory.unshift(search);
    searchHistory = searchHistory.slice(0, this._searchHistoryLimit);

    localStorage.setItem(this._searchHistoryKey, JSON.stringify(searchHistory));
  }

  public clearHistory(): void {
    localStorage.removeItem(this._searchHistoryKey);
  }
}

import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';


@Component({
  selector: 'app-sensor-location-selector',
  templateUrl: './sensor-location-selector.component.html',
  styleUrls: ['./sensor-location-selector.component.scss']
})
export class SensorLocationSelectorComponent implements OnInit {
  @Input() possibleLocations: Set<string>;
  @Output() selectedLocations: EventEmitter<Set<string>> = new EventEmitter<Set<string>>();

  selectableLocations: Map<string, boolean>;
  isSelectedCount: number;

  ngOnInit(): void {
    this.selectableLocations = new Map<string, boolean>();
    this.possibleLocations.forEach((locationName) => this.selectableLocations.set(locationName, false));
    this.setAllLocations(false);
  }

  toggleLocation(locationName: string): void {
    const newLocationSelection: boolean = !this.selectableLocations.get(locationName);
    this.selectableLocations.set(locationName, newLocationSelection);
    if (newLocationSelection) {
      this.isSelectedCount++;
    } else {
      this.isSelectedCount--;
    }
    this.emitLocationSelectionChange();
  }

  selectAllLocations(): void {
    this.setAllLocations(true);
    this.emitLocationSelectionChange();
  }

  clearAllLocations(): void {
    this.setAllLocations(false);
    this.emitLocationSelectionChange();
  }

  private setAllLocations(selection: boolean) {
    this.selectableLocations.forEach((_, location: string) => this.selectableLocations.set(location, selection));
    if (selection) {
        this.isSelectedCount = this.selectableLocations.size;
    } else {
      this.isSelectedCount = 0;
    }
  }

  isSelectionFull(): boolean {
    return this.isSelectedCount === this.possibleLocations.size && this.possibleLocations.size !== 0;
  }

  isSelectionEmpty(): boolean {
    return this.isSelectedCount === 0;
  }

  private emitLocationSelectionChange() {
    const locationSelection: Set<string> = new Set();
    this.selectableLocations.forEach((isSelected: boolean, location: string) => {
      if (isSelected) {locationSelection.add(location); } });

    this.selectedLocations.emit(locationSelection);
  }
}

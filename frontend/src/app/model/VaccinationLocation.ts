export interface VaccinationLocation {
  id: string;
  address: string;
  district: string;
  email?: string;
  notes?: string;
  phoneNumber?: string;
  zipCode: string;
}

export interface MapLocation {
  x: number;
  y: number;
}

export const defaultMapLocation: MapLocation = { x: 14.4344337, y: 50.0990584 };

export const defaultLocation = {
  id: 'default',
  address: 'Strossmayerovo náměstí 4',
  district: 'Praha 7',
  email: 'ockovani@praha7.cz',
  phoneNumber: '+420 734 521 840',
  zipCode: '170 00'
};

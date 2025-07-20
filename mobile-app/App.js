/*
 * Fleet Management System with Maxvision SDK Integration
 * 
 * Project: Fleet Management System - Mobile App
 * Client: SiniTechnologie / Sinigroupe
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This code is proprietary until transfer to client.
 */

import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Alert,
  Modal,
  RefreshControl,
  StatusBar,
  SafeAreaView,
} from 'react-native';
import { Picker } from '@react-native-picker/picker';

const API_BASE_URL = 'http://your-server-ip:6062'; // Update with your server IP

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [balises, setBalises] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [assignModalVisible, setAssignModalVisible] = useState(false);
  const [selectedBalise, setSelectedBalise] = useState('');
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [showCmaCgmForm, setShowCmaCgmForm] = useState(false);

  // CMA-CGM form data
  const [cmaCgmData, setCmaCgmData] = useState({
    equipmentReference: '',
    carrierBookingReference: '',
    transportOrder: '',
    modeOfTransport: 'TRUCK',
    partnerName: 'SINI TRANSPORT',
    transportationPhase: 'IMPORT',
    locationCode: 'BAMAKO_DEPOT_01',
    locationName: 'Bamako Central Depot',
    facilityAddress: 'Avenue de la Nation, 123',
    facilityCity: 'BAMAKO',
  });

  const [assignmentNotes, setAssignmentNotes] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadDashboard(),
        loadBalises(),
        loadCustomers(),
      ]);
    } catch (error) {
      console.error('Error loading data:', error);
    }
    setLoading(false);
  };

  const loadDashboard = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/balises/stats`);
      const data = await response.json();
      setStats(data);
    } catch (error) {
      console.error('Error loading dashboard:', error);
    }
  };

  const loadBalises = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/balises`);
      const data = await response.json();
      setBalises(data);
    } catch (error) {
      console.error('Error loading balises:', error);
    }
  };

  const loadCustomers = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/customers`);
      const data = await response.json();
      setCustomers(data);
    } catch (error) {
      console.error('Error loading customers:', error);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  const handleAssignBalise = async () => {
    if (!selectedBalise || !selectedCustomer) {
      Alert.alert('Error', 'Please select both balise and customer');
      return;
    }

    const assignmentData = {
      balise_id: selectedBalise,
      customer: selectedCustomer,
      notes: assignmentNotes,
    };

    // Add CMA-CGM specific data if selected
    if (selectedCustomer === 'CMACGM') {
      if (!cmaCgmData.equipmentReference || !cmaCgmData.carrierBookingReference) {
        Alert.alert('Error', 'Please fill in required CMA-CGM fields');
        return;
      }
      assignmentData.cmacgm_data = cmaCgmData;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/balises/assign`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(assignmentData),
      });

      if (response.ok) {
        Alert.alert('Success', 'Balise assigned successfully!');
        setAssignModalVisible(false);
        resetAssignmentForm();
        await loadData();
      } else {
        Alert.alert('Error', 'Failed to assign balise');
      }
    } catch (error) {
      Alert.alert('Error', 'Network error: ' + error.message);
    }
  };

  const resetAssignmentForm = () => {
    setSelectedBalise('');
    setSelectedCustomer('');
    setShowCmaCgmForm(false);
    setAssignmentNotes('');
    setCmaCgmData({
      equipmentReference: '',
      carrierBookingReference: '',
      transportOrder: '',
      modeOfTransport: 'TRUCK',
      partnerName: 'SINI TRANSPORT',
      transportationPhase: 'IMPORT',
      locationCode: 'BAMAKO_DEPOT_01',
      locationName: 'Bamako Central Depot',
      facilityAddress: 'Avenue de la Nation, 123',
      facilityCity: 'BAMAKO',
    });
  };

  const triggerManualSync = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/cmacgm/sync`, {
        method: 'POST',
      });

      if (response.ok) {
        Alert.alert('Success', 'Manual sync triggered successfully!');
      } else {
        Alert.alert('Error', 'Failed to trigger sync');
      }
    } catch (error) {
      Alert.alert('Error', 'Network error: ' + error.message);
    }
  };

  const renderDashboard = () => (
    <ScrollView
      style={styles.container}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
    >
      <View style={styles.statsContainer}>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{stats.total || 0}</Text>
          <Text style={styles.statLabel}>Total Balises</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{stats.active || 0}</Text>
          <Text style={styles.statLabel}>Active</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{stats.cmacgm || 0}</Text>
          <Text style={styles.statLabel}>CMA-CGM</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{stats.unassigned || 0}</Text>
          <Text style={styles.statLabel}>Unassigned</Text>
        </View>
      </View>

      <TouchableOpacity
        style={styles.assignButton}
        onPress={() => setAssignModalVisible(true)}
      >
        <Text style={styles.assignButtonText}>🎯 Assign Balise to Customer</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.syncButton}
        onPress={triggerManualSync}
      >
        <Text style={styles.syncButtonText}>🔄 Trigger Manual Sync</Text>
      </TouchableOpacity>
    </ScrollView>
  );

  const renderBalises = () => (
    <ScrollView
      style={styles.container}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
    >
      {balises.map((balise) => (
        <View key={balise.id} style={styles.baliseCard}>
          <View style={styles.baliseHeader}>
            <Text style={styles.baliseName}>{balise.name}</Text>
            <View style={[
              styles.statusBadge,
              balise.status === 'ACTIVE' ? styles.statusActive : styles.statusInactive
            ]}>
              <Text style={styles.statusText}>{balise.status}</Text>
            </View>
          </View>
          <Text style={styles.baliseDetail}>IMEI: {balise.imei}</Text>
          <Text style={styles.baliseDetail}>Customer: {balise.customer || 'Unassigned'}</Text>
          <Text style={styles.baliseDetail}>
            Battery: {balise.battery_level ? `${balise.battery_level}%` : 'Unknown'}
          </Text>
          <Text style={styles.baliseDetail}>
            Last Seen: {balise.last_seen ? new Date(balise.last_seen).toLocaleString() : 'Never'}
          </Text>
        </View>
      ))}
    </ScrollView>
  );

  const renderAssignmentModal = () => (
    <Modal
      animationType="slide"
      transparent={false}
      visible={assignModalVisible}
      onRequestClose={() => setAssignModalVisible(false)}
    >
      <SafeAreaView style={styles.modalContainer}>
        <View style={styles.modalHeader}>
          <TouchableOpacity onPress={() => setAssignModalVisible(false)}>
            <Text style={styles.modalCloseButton}>✕</Text>
          </TouchableOpacity>
          <Text style={styles.modalTitle}>Assign Balise</Text>
          <View style={{ width: 30 }} />
        </View>

        <ScrollView style={styles.modalContent}>
          <View style={styles.formGroup}>
            <Text style={styles.formLabel}>Select Balise</Text>
            <Picker
              selectedValue={selectedBalise}
              onValueChange={setSelectedBalise}
              style={styles.picker}
            >
              <Picker.Item label="Choose balise..." value="" />
              {balises.filter(b => !b.customer).map(balise => (
                <Picker.Item
                  key={balise.id}
                  label={`${balise.name} (${balise.imei})`}
                  value={balise.id}
                />
              ))}
            </Picker>
          </View>

          <View style={styles.formGroup}>
            <Text style={styles.formLabel}>Select Customer</Text>
            <Picker
              selectedValue={selectedCustomer}
              onValueChange={(value) => {
                setSelectedCustomer(value);
                setShowCmaCgmForm(value === 'CMACGM');
              }}
              style={styles.picker}
            >
              <Picker.Item label="Choose customer..." value="" />
              <Picker.Item label="CMA-CGM" value="CMACGM" />
              <Picker.Item label="DHL" value="DHL" />
              <Picker.Item label="Maersk" value="MAERSK" />
              <Picker.Item label="Other Customer" value="OTHER" />
            </Picker>
          </View>

          {showCmaCgmForm && (
            <View style={styles.cmacgmForm}>
              <Text style={styles.cmacgmTitle}>🚢 CMA-CGM Container Info</Text>
              
              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Equipment Reference *</Text>
                <TextInput
                  style={styles.textInput}
                  value={cmaCgmData.equipmentReference}
                  onChangeText={(text) => setCmaCgmData({...cmaCgmData, equipmentReference: text})}
                  placeholder="e.g., APZU2106333"
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Carrier Booking Reference *</Text>
                <TextInput
                  style={styles.textInput}
                  value={cmaCgmData.carrierBookingReference}
                  onChangeText={(text) => setCmaCgmData({...cmaCgmData, carrierBookingReference: text})}
                  placeholder="e.g., LHV3076333"
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Transport Order</Text>
                <TextInput
                  style={styles.textInput}
                  value={cmaCgmData.transportOrder}
                  onChangeText={(text) => setCmaCgmData({...cmaCgmData, transportOrder: text})}
                  placeholder="e.g., TLHV2330333"
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Mode of Transport</Text>
                <Picker
                  selectedValue={cmaCgmData.modeOfTransport}
                  onValueChange={(value) => setCmaCgmData({...cmaCgmData, modeOfTransport: value})}
                  style={styles.picker}
                >
                  <Picker.Item label="Truck" value="TRUCK" />
                  <Picker.Item label="Rail" value="RAIL" />
                  <Picker.Item label="Barge" value="BARGE" />
                  <Picker.Item label="Vessel" value="VESSEL" />
                </Picker>
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Partner Name</Text>
                <TextInput
                  style={styles.textInput}
                  value={cmaCgmData.partnerName}
                  onChangeText={(text) => setCmaCgmData({...cmaCgmData, partnerName: text})}
                  placeholder="Partner company name"
                />
              </View>

              <Text style={styles.sectionTitle}>📍 Location (Bamako, Mali)</Text>
              
              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Location Code</Text>
                <TextInput
                  style={styles.textInput}
                  value={cmaCgmData.locationCode}
                  onChangeText={(text) => setCmaCgmData({...cmaCgmData, locationCode: text})}
                  placeholder="Location identifier"
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Location Name</Text>
                <TextInput
                  style={styles.textInput}
                  value={cmaCgmData.locationName}
                  onChangeText={(text) => setCmaCgmData({...cmaCgmData, locationName: text})}
                  placeholder="Location display name"
                />
              </View>
            </View>
          )}

          <View style={styles.formGroup}>
            <Text style={styles.formLabel}>Assignment Notes</Text>
            <TextInput
              style={[styles.textInput, styles.textArea]}
              value={assignmentNotes}
              onChangeText={setAssignmentNotes}
              placeholder="Optional notes about this assignment..."
              multiline
              numberOfLines={3}
            />
          </View>

          <TouchableOpacity style={styles.submitButton} onPress={handleAssignBalise}>
            <Text style={styles.submitButtonText}>🎯 Assign Balise</Text>
          </TouchableOpacity>
        </ScrollView>
      </SafeAreaView>
    </Modal>
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" backgroundColor="#2c3e50" />
      
      <View style={styles.header}>
        <Text style={styles.headerTitle}>🚛 Fleet Monitor</Text>
        <Text style={styles.headerSubtitle}>Balise Management</Text>
      </View>

      <View style={styles.tabContainer}>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'dashboard' && styles.activeTab]}
          onPress={() => setActiveTab('dashboard')}
        >
          <Text style={[styles.tabText, activeTab === 'dashboard' && styles.activeTabText]}>
            📊 Dashboard
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'balises' && styles.activeTab]}
          onPress={() => setActiveTab('balises')}
        >
          <Text style={[styles.tabText, activeTab === 'balises' && styles.activeTabText]}>
            📡 Balises
          </Text>
        </TouchableOpacity>
      </View>

      {activeTab === 'dashboard' && renderDashboard()}
      {activeTab === 'balises' && renderBalises()}
      {renderAssignmentModal()}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#2c3e50',
  },
  header: {
    backgroundColor: '#2c3e50',
    padding: 20,
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 5,
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#bdc3c7',
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: '#34495e',
  },
  tab: {
    flex: 1,
    padding: 15,
    alignItems: 'center',
  },
  activeTab: {
    backgroundColor: 'white',
  },
  tabText: {
    color: '#bdc3c7',
    fontWeight: '600',
  },
  activeTabText: {
    color: '#2c3e50',
  },
  container: {
    flex: 1,
    backgroundColor: '#ecf0f1',
    padding: 15,
  },
  statsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  statCard: {
    backgroundColor: '#3498db',
    padding: 20,
    borderRadius: 10,
    width: '48%',
    marginBottom: 10,
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 32,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 5,
  },
  statLabel: {
    fontSize: 14,
    color: 'white',
    opacity: 0.9,
  },
  assignButton: {
    backgroundColor: '#27ae60',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
    marginBottom: 10,
  },
  assignButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
  },
  syncButton: {
    backgroundColor: '#f39c12',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
  },
  syncButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
  },
  baliseCard: {
    backgroundColor: 'white',
    padding: 20,
    borderRadius: 10,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  baliseHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  baliseName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 15,
  },
  statusActive: {
    backgroundColor: '#d4edda',
  },
  statusInactive: {
    backgroundColor: '#f8d7da',
  },
  statusText: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#2c3e50',
  },
  baliseDetail: {
    fontSize: 14,
    color: '#7f8c8d',
    marginBottom: 5,
  },
  modalContainer: {
    flex: 1,
    backgroundColor: 'white',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#2c3e50',
  },
  modalCloseButton: {
    fontSize: 24,
    color: 'white',
    fontWeight: 'bold',
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: 'white',
  },
  modalContent: {
    flex: 1,
    padding: 20,
  },
  formGroup: {
    marginBottom: 20,
  },
  formLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2c3e50',
    marginBottom: 8,
  },
  textInput: {
    borderWidth: 2,
    borderColor: '#bdc3c7',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    backgroundColor: 'white',
  },
  textArea: {
    height: 80,
    textAlignVertical: 'top',
  },
  picker: {
    borderWidth: 2,
    borderColor: '#bdc3c7',
    borderRadius: 8,
    backgroundColor: 'white',
  },
  cmacgmForm: {
    backgroundColor: '#fff3cd',
    padding: 20,
    borderRadius: 10,
    marginBottom: 20,
    borderWidth: 2,
    borderColor: '#f39c12',
  },
  cmacgmTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginBottom: 15,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginTop: 15,
    marginBottom: 10,
  },
  submitButton: {
    backgroundColor: '#3498db',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 20,
  },
  submitButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
  },
});

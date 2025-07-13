import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Container, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, CircularProgress, Box } from '@mui/material';

function App() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Determine API URL dynamically
    const apiUrl = process.env.REACT_APP_API_URL || `${window.location.protocol}//${window.location.hostname}:6060/api`;
    axios.get(`${apiUrl}/reports`)
      .then(res => {
        setReports(res.data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>Device Monitor Dashboard</Typography>
      {loading ? (
        <Box display="flex" justifyContent="center" alignItems="center" height={200}>
          <CircularProgress />
        </Box>
      ) : (
        <Paper>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Lock Code</TableCell>
                  <TableCell>Command Type</TableCell>
                  <TableCell>Received At</TableCell>
                  <TableCell>Raw Data</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {reports.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>{r.id}</TableCell>
                    <TableCell>{r.lockCode || '-'}</TableCell>
                    <TableCell>{r.commandType || '-'}</TableCell>
                    <TableCell>{r.receivedAt || '-'}</TableCell>
                    <TableCell style={{ maxWidth: 250, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{r.reportData}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}
    </Container>
  );
}

export default App;

import * as React from 'react';
import * as ReactDOM from 'react-dom/client';
import { useState } from 'react';
import './App.css';
import { useEffect } from 'react';
import { ClientControllerService, OpenAPI, TorrentDTO } from './client';

function App() {
  const [torrents, setTorrents] = useState<TorrentDTO[]>([]);

  useEffect(() => {
    const interval = setInterval(async () => {
      var torrents = await ClientControllerService.getTorrents();
      setTorrents(torrents);
    }, 1_000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className='container-fluid'>
      <div className='row'>
        <nav id='sidebarMenu' className='col-md-3 col-lg-2 d-md-block bg-light sidebar collapse'>
          <div className="position-sticky pt-3">
            <ul className="nav flex-column">
              <li className="nav-item">
                <a className="nav-link active" aria-current="page" href="#">
                  <span data-feather="home" className="align-text-bottom"></span>
                  Dashboard
                </a>
              </li>
            </ul>
          </div>
        </nav>

        <main className="col-md-9 ms-sm-auto col-lg-10 px-md-4">
          <div className="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h1 className="h2">Dashboard</h1>
          </div>

          <h2>Torrents</h2>
          <div className="table-responsive">
            <table className="table table-striped table-sm">
              <thead>
                <tr>
                  <th scope="col">Name</th>
                  <th scope="col">Size</th>
                  <th scope="col">Progress</th>
                  <th scope="col">Status</th>
                  <th scope="col">Seeds</th>
                  <th scope="col">Peers</th>
                  <th scope="col">Download Rate</th>
                  <th scope="col">Upload Rate</th>
                </tr>
              </thead>
              <tbody>
                {torrents.map(torrent =>
                  <tr>
                    <td>{torrent.name}</td>
                    <td>{torrent.size}</td>
                    <td>{torrent.progress}</td>
                    <td>{torrent.status}</td>
                    <td>{torrent.seeds}</td>
                    <td>{torrent.peers}</td>
                    <td>{torrent.downloadRate}</td>
                    <td>{torrent.uploadRate}</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </main>
      </div>
    </div>
  );
}

export default App;

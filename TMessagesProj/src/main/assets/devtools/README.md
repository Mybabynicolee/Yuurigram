# Yuurigram DevTools

The on-device inspector UI is an Android-hosted DevTools-style adapter. It keeps the active Mini App WebView alive and communicates with it through a process-local JavaScript bridge. It does not open a LAN port, start a bot, embed a bot token, or require Telegram Desktop.

The interface and terminology are designed to host a Chromium DevTools frontend adapter. The upstream Chromium DevTools frontend source and protocol documentation are maintained by the Chromium project:

- https://github.com/ChromeDevTools/devtools-frontend
- https://chromedevtools.github.io/devtools-protocol/

Upstream DevTools frontend licensing is BSD-3-Clause. The current Android adapter intentionally implements the on-device subset that can be safely mediated by Android WebView APIs: DOM snapshot, page URL/query, console capture after inspector injection, resource timing, storage inspection, JavaScript evaluation, and reload. A full unmodified Chromium DevTools frontend requires Chromium's generated build artifacts and a CDP transport host; those are not silently claimed here.

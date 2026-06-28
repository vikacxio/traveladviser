import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ChatMessage {
  sender: string;
  text: string;
  time: string;
  self?: boolean;
}

interface ChatRoom {
  id: string;
  name: string;
  members: number;
  messages: ChatMessage[];
}

@Component({
  standalone: true,
  selector: 'app-chat',
  imports: [CommonModule, FormsModule],
  template: `
    <section class="space-y-6">
      <div class="rounded-3xl bg-white p-6 shadow-sm">
        <h1 class="text-2xl font-semibold text-slate-900">Travel Chat Rooms</h1>
        <p class="mt-2 text-slate-600">Join multiple rooms, discuss itineraries, and coordinate your trip in one place.</p>
      </div>

      <div class="grid gap-6 lg:grid-cols-[320px_1fr]">
        <aside class="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-4 flex items-center justify-between">
            <h2 class="text-lg font-semibold text-slate-900">Rooms</h2>
            <button type="button" class="rounded-xl bg-slate-900 px-3 py-2 text-xs font-semibold text-white hover:bg-slate-700">New</button>
          </div>

          <div class="space-y-2">
            <button
              *ngFor="let room of rooms"
              type="button"
              (click)="selectRoom(room)"
              class="w-full rounded-xl border p-3 text-left transition"
              [class.border-slate-900]="activeRoom?.id === room.id"
              [class.bg-slate-50]="activeRoom?.id === room.id"
              [class.border-slate-200]="activeRoom?.id !== room.id">
              <p class="font-medium text-slate-900">{{ room.name }}</p>
              <p class="text-xs text-slate-500">{{ room.members }} members</p>
            </button>
          </div>
        </aside>

        <div class="rounded-3xl border border-slate-200 bg-white shadow-sm">
          <div class="border-b border-slate-200 px-5 py-4">
            <h3 class="text-lg font-semibold text-slate-900">{{ activeRoom?.name }}</h3>
            <p class="text-sm text-slate-500">{{ activeRoom?.members }} members active</p>
          </div>

          <div class="h-[420px] space-y-3 overflow-y-auto p-5">
            <div *ngFor="let message of activeRoom?.messages" class="flex" [class.justify-end]="message.self">
              <div class="max-w-[80%] rounded-2xl px-4 py-3" [class.bg-slate-900]="message.self" [class.text-white]="message.self" [class.bg-slate-100]="!message.self" [class.text-slate-900]="!message.self">
                <p class="text-xs opacity-80">{{ message.sender }} • {{ message.time }}</p>
                <p class="mt-1 text-sm">{{ message.text }}</p>
              </div>
            </div>
          </div>

          <div class="border-t border-slate-200 p-4">
            <div class="flex gap-3">
              <input
                [(ngModel)]="draftMessage"
                (keyup.enter)="sendMessage()"
                placeholder="Type a message..."
                class="w-full rounded-xl border border-slate-300 px-4 py-2 focus:border-slate-900 focus:outline-none" />
              <button type="button" (click)="sendMessage()" class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700">
                Send
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  `
})
export class ChatComponent {
  rooms: ChatRoom[] = [
    {
      id: 'goa-planning',
      name: 'Goa Planning',
      members: 6,
      messages: [
        { sender: 'Ankit', text: 'Can we finalize scooty rentals by tonight?', time: '7:10 PM' },
        { sender: 'You', text: 'Yes, I can book 3 by 9 PM.', time: '7:12 PM', self: true }
      ]
    },
    {
      id: 'weekend-delhi',
      name: 'Delhi Weekend',
      members: 4,
      messages: [
        { sender: 'Riya', text: 'Let us start from India Gate then Humayun Tomb.', time: '6:25 PM' }
      ]
    },
    {
      id: 'budget-tips',
      name: 'Budget Tips',
      members: 18,
      messages: [
        { sender: 'Admin', text: 'Share hostel and food hacks for backpackers.', time: '5:40 PM' }
      ]
    }
  ];

  activeRoom: ChatRoom = this.rooms[0];
  draftMessage = '';

  selectRoom(room: ChatRoom): void {
    this.activeRoom = room;
  }

  sendMessage(): void {
    const text = this.draftMessage.trim();
    if (!text) return;

    this.activeRoom.messages = [
      ...this.activeRoom.messages,
      {
        sender: 'You',
        text,
        time: new Date().toLocaleTimeString('en-IN', { hour: 'numeric', minute: '2-digit' }),
        self: true
      }
    ];
    this.draftMessage = '';
  }
}
